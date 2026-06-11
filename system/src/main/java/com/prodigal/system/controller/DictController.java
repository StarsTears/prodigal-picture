package com.prodigal.system.controller;

import com.prodigal.system.annotation.PermissionCheck;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.model.dto.dict.DictDTO;
import com.prodigal.system.model.entity.Dict;
import com.prodigal.system.service.DictService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dict")
public class DictController {

    @Resource
    private DictService dictService;

    @GetMapping("/types")
    public BaseResult<List<String>> listDictTypes() {
        return ResultUtils.success(dictService.listDictTypes());
    }

    @GetMapping("/list/{dictType}")
    public BaseResult<List<Dict>> listByType(@PathVariable("dictType") String dictType) {
        return ResultUtils.success(dictService.listByType(dictType));
    }

    @PostMapping("/add")
    @PermissionCheck(mustRole = {"admin", "administrator"})
    public BaseResult<Boolean> add(@RequestBody DictDTO dto) {
        dictService.add(dto);
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    @PermissionCheck(mustRole = {"admin", "administrator"})
    public BaseResult<Boolean> update(@RequestBody DictDTO dto) {
        dictService.update(dto);
        return ResultUtils.success(true);
    }

    @PostMapping("/delete")
    @PermissionCheck(mustRole = {"admin", "administrator"})
    public BaseResult<Boolean> delete(@RequestBody DictDTO dto) {
        dictService.delete(dto.getId());
        return ResultUtils.success(true);
    }
}
