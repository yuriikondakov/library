package ua.edu.springLibrary.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ua.edu.springLibrary.domain.Book;
import ua.edu.springLibrary.domain.BookTracking;
import ua.edu.springLibrary.domain.User;
import ua.edu.springLibrary.service.BookService;
import ua.edu.springLibrary.service.BookTrackingService;
import ua.edu.springLibrary.service.UserService;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Controller
@RequiredArgsConstructor
@Scope("session")
public class BookController {
    private final BookService bookService;
    private final BookTrackingService bookTrackingService;
    private final UserService userService;

    @GetMapping(value = "/book")
    public ModelAndView getAllBooks(@RequestParam("page") Integer page,
                                    @RequestParam("size") Integer size) {
        ModelAndView modelAndView = new ModelAndView();
        log.debug("Get book by id");
        int currentPage = (page == null) ? 1 : page;
        int pageSize = (size == null) ? 10 : size;
        Page<Book> bookPage = bookService.findPaginated(PageRequest.of(currentPage - 1, pageSize));
        modelAndView.addObject("bookPage", bookPage);

        int totalPages = bookPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            modelAndView.addObject("pageNumbers", pageNumbers);
        }
        modelAndView.setViewName("catalog");
        return modelAndView;
    }

    @GetMapping("/book/{id}")
    public String getBookById(@PathVariable("id") Integer bookId, Model model) {
        User user = getUser();
        log.debug(user.toString());
        if (user.getBookIds().stream().anyMatch(b -> b.equals(bookId))) {
            model.addAttribute("bookIsAlreadyTaken", true);
        }
        model.addAttribute("book", bookService.findById(bookId));
        return "book";
    }

    @PostMapping("/book/{id}")
    public String saveBook(@PathVariable("id") Integer bookId, Model model) {
        bookTrackingService.saveBookTracking(getUser(), bookId);
        model.addAttribute("takeBookSuccessful", true);
        return "redirect:/book/{id}";
    }

    @GetMapping(value = "/mybooks")
    public String getUserBooks(Model model) {
        Map<BookTracking, Book> userBookTrackingToUserBook = new HashMap<>();
        for (Integer bookTrackingId : getUser().getBookTrackingIds()) {
            BookTracking bookTracking = bookTrackingService.findById(bookTrackingId);
            userBookTrackingToUserBook.put(bookTracking, bookTracking.getBook());
        }
        model.addAttribute("userBooksMap", userBookTrackingToUserBook);
        return "user_books";
    }

    @GetMapping("/returnbook/{id}")
    public String returnBook(@PathVariable("id") Integer bookTrackingId, Model model) {
        bookTrackingService.returnBook(bookTrackingId);
        model.addAttribute("returnBookSuccessful", true);
        return "redirect:/mybooks";
    }

    private User getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByEmail(auth.getName());
    }

    @GetMapping("/init")
    private String init(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        user.setPassword("");
        session.setAttribute("user", user);
        return "redirect:/mybooks";
    }
}
