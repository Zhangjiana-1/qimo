package com.example.qimo.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityNotFoundException;

/**
 * 全局异常处理器，统一处理应用中的异常
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理实体未找到异常
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ModelAndView handleEntityNotFoundException(EntityNotFoundException ex, Model model) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("error/error");
        modelAndView.addObject("statusCode", 404);
        modelAndView.addObject("errorMessage", ex.getMessage());
        modelAndView.addObject("pageTitle", "资源未找到");
        return modelAndView;
    }

    /**
     * 处理其他所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(Exception ex, Model model) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("error/error");
        modelAndView.addObject("statusCode", 500);
        modelAndView.addObject("errorMessage", "服务器内部错误，请稍后再试");
        modelAndView.addObject("pageTitle", "服务器错误");
        // 在开发环境可以输出详细错误信息到控制台
        ex.printStackTrace();
        return modelAndView;
    }
}