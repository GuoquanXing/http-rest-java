package com.sap.cc.redirect;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

/***
 * Source Doc: https://www.baeldung.com/spring-redirect-and-forward
 */
@RestController
@RequestMapping("/redirect")
public class RedirectController {

    @GetMapping("/redirectWithRedirectView")
    public RedirectView redirectWithUsingRedirectView(
            RedirectAttributes attributes) {
        attributes.addFlashAttribute("flashAttribute", "redirectWithRedirectView");
        attributes.addAttribute("attribute", "redirectWithRedirectView");
        return new RedirectView("targetView");
    }

    @GetMapping("/redirectWithRedirectPrefix")
    public ModelAndView redirectWithUsingRedirectPrefix(ModelMap model) {
        model.addAttribute("attribute", "redirectWithRedirectPrefix");
        return new ModelAndView("redirect:targetView", model);
    }

    @GetMapping("/forwardWithForwardPrefix")
    public ModelAndView redirectWithUsingForwardPrefix(ModelMap model) {
        model.addAttribute("attribute", "forwardWithForwardPrefix");
        return new ModelAndView("forward:targetView", model);
    }

    @GetMapping("/targetView")
    public ResponseEntity<String> redirectTarget() {
        return ResponseEntity.status(HttpStatus.OK).body("{'msg':'This is the Target View'}");
    }

    @PostMapping("/redirectPostToGet")
    public ModelAndView redirectPostToGet(HttpServletRequest request) {
        return new ModelAndView("redirect:targetView");
    }

    @PostMapping("/redirectPostToPost")
    public ModelAndView redirectPostToPost(HttpServletRequest request) {
        request.setAttribute(
                View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
        return new ModelAndView("redirect:/redirectedPostToPost");
    }

    @PostMapping("/redirectedPostToPost")
    public ModelAndView redirectedPostToPost() {
        return new ModelAndView("targetView");
    }
}
