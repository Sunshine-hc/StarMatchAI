package com.hc.starmatchai.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 请求参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验异常: {}", message);
        return Result.error(400, message);
    }

    /**
     * 请求参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数绑定异常: {}", message);
        return Result.error(400, message);
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验异常: {}", message);
        return Result.error(400, message);
    }

    /**
     * 请求方式不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.error("不支持的请求方式: {}", e.getMessage());
        return Result.error(405, "请求方式不支持，" + e.getMessage());
    }

    /**
     * 媒体类型不支持异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<Void> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.error("不支持的媒体类型: {}", e.getMessage());
        return Result.error(415, "媒体类型不支持");
    }

    /**
     * 媒体类型不接受异常
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public Result<Void> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException e) {
        log.error("不接受的媒体类型: {}", e.getMessage());
        return Result.error(406, "不接受的媒体类型");
    }

    /**
     * 请求参数缺失异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        log.error("请求参数缺失: {}", e.getMessage());
        return Result.error(400, "请求参数缺失: " + e.getParameterName());
    }

    /**
     * 路径变量缺失异常
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public Result<Void> handleMissingPathVariable(MissingPathVariableException e) {
        log.error("路径变量缺失: {}", e.getMessage());
        return Result.error(400, "路径变量缺失: " + e.getVariableName());
    }

    /**
     * 请求部分缺失异常
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public Result<Void> handleMissingServletRequestPart(MissingServletRequestPartException e) {
        log.error("请求部分缺失: {}", e.getMessage());
        return Result.error(400, "请求部分缺失: " + e.getRequestPartName());
    }

    /**
     * 类型不匹配异常
     */
    @ExceptionHandler(TypeMismatchException.class)
    public Result<Void> handleTypeMismatch(TypeMismatchException e) {
        log.error("参数类型不匹配: {}", e.getMessage());
        return Result.error(400, "参数类型不匹配: " + e.getPropertyName());
    }

    /**
     * 请求体读取异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.error("请求体读取失败: {}", e.getMessage());
        return Result.error(400, "请求体格式错误");
    }

    /**
     * 响应体写入异常
     */
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public Result<Void> handleHttpMessageNotWritable(HttpMessageNotWritableException e) {
        log.error("响应体写入失败: {}", e.getMessage());
        return Result.error(500, "响应体写入失败");
    }

    /**
     * 请求绑定异常
     */
    @ExceptionHandler(ServletRequestBindingException.class)
    public Result<Void> handleServletRequestBinding(ServletRequestBindingException e) {
        log.error("请求绑定失败: {}", e.getMessage());
        return Result.error(400, "请求绑定失败");
    }

    /**
     * 转换不支持异常
     */
    @ExceptionHandler(ConversionNotSupportedException.class)
    public Result<Void> handleConversionNotSupported(ConversionNotSupportedException e) {
        log.error("类型转换失败: {}", e.getMessage());
        return Result.error(500, "类型转换失败");
    }

    /**
     * 404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Void> handleNoHandlerFound(NoHandlerFoundException e) {
        log.error("请求地址不存在: {}", e.getMessage());
        return Result.error(404, "请求地址不存在");
    }

    /**
     * 异步请求超时异常
     */
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public Result<Void> handleAsyncRequestTimeout(AsyncRequestTimeoutException e) {
        log.error("异步请求超时: {}", e.getMessage());
        return Result.error(503, "服务暂时不可用，请稍后重试");
    }

    /**
     * 数据库唯一约束异常
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<Void> handleSQLIntegrityConstraintViolation(SQLIntegrityConstraintViolationException e) {
        log.error("数据库唯一约束异常: {}", e.getMessage());
        return Result.error(400, "数据已存在");
    }

    /**
     * 其他未处理的异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(500, "系统异常，请稍后重试");
    }
}