package com.prodigal.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.mapper.DictMapper;
import com.prodigal.system.model.dto.dict.DictDTO;
import com.prodigal.system.model.entity.Dict;
import com.prodigal.system.service.DictService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Override
    public List<Dict> listByType(String dictType) {
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<Dict>()
                .eq(Dict::getDictType, dictType)
                .orderByAsc(Dict::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<String> listDictTypes() {
        return this.list().stream()
                .map(Dict::getDictType)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public void add(DictDTO dto) {
        Dict dict = new Dict();
        BeanUtils.copyProperties(dto, dict);
        if (!save(dict)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "添加失败");
        }
    }

    @Override
    public void update(DictDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id不能为空");
        }
        Dict dict = new Dict();
        BeanUtils.copyProperties(dto, dict);
        if (!updateById(dict)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新失败");
        }
    }

    @Override
    public void delete(Long id) {
        if (!removeById(id)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败");
        }
    }
}
