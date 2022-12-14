package com.bright.card.controller;

import com.bright.card.consts.ReturnCodeConsts;
import com.bright.card.controller.dto.base.BaseEntityDTO;
import com.bright.card.controller.dto.base.BasePageDTO;
import com.bright.card.util.MessageBuilder;
import com.github.pagehelper.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

/**
 * Controller 基础类
 */
@SuppressWarnings("all")
@Slf4j
public abstract class BaseController implements MessageBuilder {
    @Autowired
    private MessageSource messageSource;

    @Override
    public MessageSource getMessageSource() {
        return messageSource;
    }

    /**
     * 成功删除后的返回数据
     *
     * @param delNum 删除的条数
     * @return
     */
    protected <T> BaseEntityDTO<T> successDeleteReturn(int delNum) {
        return this.buildEntityDTO(ReturnCodeConsts.CODE_SUCCESS_000002, new String[]{String.valueOf(delNum)});
    }

    /**
     * 成功执行后的封装返回
     *
     * @param data
     * @return
     */
    protected <T> BaseEntityDTO<T> successReturn(T entity, String... args) {
        return this.buildEntityDTO(entity, ReturnCodeConsts.CODE_SUCCESS, args);
    }

    /**
     * 执行后返回
     *
     * @param entity
     * @param code
     * @param args
     * @param <T>
     * @return
     */
    protected <T> BaseEntityDTO<T> successReturn(String code, T entity, String... args) {
        return this.buildEntityDTO(entity, code, args);
    }

    /**
     * MSG封装返回
     *
     * @return
     */
    protected <T> BaseEntityDTO<T> msgReturn(String code, String... args) {
        return this.buildEntityDTO(code, args);
    }

    /**
     * 封装page
     *
     * @param page
     * @return
     */
    protected <T> BasePageDTO<T> pageReturn(Page<T> page) {
        return this.buildPageDTO(page.getResult(), page.getTotal(), ReturnCodeConsts.CODE_SUCCESS);
    }
}
