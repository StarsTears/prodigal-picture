package com.prodigal.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.prodigal.system.model.dto.dict.DictDTO;
import com.prodigal.system.model.entity.Dict;

import java.util.List;

public interface DictService extends IService<Dict> {

    List<Dict> listByType(String dictType);

    List<String> listDictTypes();

    void add(DictDTO dto);

    void update(DictDTO dto);

    void delete(Long id);
}
