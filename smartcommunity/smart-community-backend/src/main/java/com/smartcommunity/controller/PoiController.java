package com.smartcommunity.controller;

import com.smartcommunity.common.Result;
import com.smartcommunity.entity.TbPoi;
import com.smartcommunity.mapper.TbPoiMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/poi")
public class PoiController {

    @Autowired
    private TbPoiMapper poiMapper;

    @GetMapping("/list")
    public Result<List<TbPoi>> list() {
        return Result.success(poiMapper.selectPoiList());
    }
}