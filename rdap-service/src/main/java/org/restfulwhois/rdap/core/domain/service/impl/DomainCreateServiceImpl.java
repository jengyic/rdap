/*
 * Copyright (c) 2012 - 2015, Internet Corporation for Assigned Names and
 * Numbers (ICANN) and China Internet Network Information Center (CNNIC)
 * 
 * All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  
 * * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * * Neither the name of the ICANN, CNNIC nor the names of its contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL ICANN OR CNNIC BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package org.restfulwhois.rdap.core.domain.service.impl;

import static org.restfulwhois.rdap.common.util.UpdateValidateUtil.MAX_LENGTH_HANDLE;
import static org.restfulwhois.rdap.common.util.UpdateValidateUtil.MAX_LENGTH_IDNTABLE;
import static org.restfulwhois.rdap.common.util.UpdateValidateUtil.MAX_LENGTH_UNICODENAME;

import java.util.List;

import org.restfulwhois.rdap.common.dto.DomainDto;
import org.restfulwhois.rdap.common.dto.SecureDnsDto;
import org.restfulwhois.rdap.common.dto.VariantDto;
import org.restfulwhois.rdap.common.model.Domain;
import org.restfulwhois.rdap.common.model.Domain.DomainType;
import org.restfulwhois.rdap.common.model.DsData;
import org.restfulwhois.rdap.common.util.UpdateValidateUtil;
import org.restfulwhois.rdap.common.validation.UpdateValidationError;
import org.restfulwhois.rdap.common.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * create service implementation.
 * 
 * @author jiashuo
 * 
 */
@Service("domainCreateServiceImpl")
public class DomainCreateServiceImpl extends DomainBaseServiceImpl {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DomainCreateServiceImpl.class);

    @Override
    protected void execute(Domain domain) {
        dao.create(domain);
    }

    @Override
    protected Domain convertDtoToModel(DomainDto dto) {
        Domain domain = super.convertDtoToModelWithoutType(dto);
        domain.setType(DomainType.getByTypeName(dto.getType()));
        return domain;
    }

    @Override
    protected ValidationResult validate(DomainDto domainDto) {
        ValidationResult validationResult =
                super.validateWithoutType(domainDto);
        checkHandleNotExistForCreate(domainDto.getHandle(), validationResult);
        checkNotEmpty(domainDto.getType(), "type", validationResult);
        checkDomainTypeValid(domainDto.getType(), "type", validationResult);
        checkVariants(domainDto, validationResult);
        checkSecureDns(domainDto, validationResult);
        return validationResult;
    }

    private void checkSecureDns(DomainDto domainDto,
            ValidationResult validationResult) {
        List<SecureDnsDto> secureDnsList = domainDto.getSecureDns();
        if (secureDnsList.isEmpty()) {
            return;
        }
        for (SecureDnsDto secureDns : secureDnsList) {
            checkMinMaxInt(secureDns.getMaxSigLife(),
                    UpdateValidateUtil.MIN_VAL_FOR_INT_COLUMN,
                    UpdateValidateUtil.MAX_VAL_FOR_INT_COLUMN,
                    "secureDns.maxSigLife", validationResult);
            checkDsData(secureDns, validationResult);

        }
    }

    private void checkDsData(SecureDnsDto secureDns,
            ValidationResult validationResult) {
        List<DsData> dsDatas = secureDns.getDsData();
        if (dsDatas.isEmpty()) {
            return;
        }
        for (DsData dsData : dsDatas) {
            checkMinMaxInt(dsData.getKeyTag(),
                    UpdateValidateUtil.MIN_VAL_FOR_INT_COLUMN,
                    UpdateValidateUtil.MAX_VAL_FOR_INT_COLUMN,
                    "secureDns.maxSigLife", validationResult);
            checkEvents(dsData.getEvents(), validationResult);

        }
    }

    private void checkVariants(DomainDto domainDto,
            ValidationResult validationResult) {
        List<VariantDto> variants = domainDto.getVariants();
        if (variants.isEmpty()) {
            return;
        }
        for (VariantDto variant : variants) {
            checkNotEmptyAndMaxLength(variant.getLdhName(), MAX_LENGTH_HANDLE,
                    "variant.ldhName", validationResult);
            checkNotEmptyAndMaxLength(variant.getUnicodeName(),
                    MAX_LENGTH_UNICODENAME, "variant.unicodeName",
                    validationResult);
            checkMaxLength(variant.getIdnTable(), MAX_LENGTH_IDNTABLE,
                    "idnTable", validationResult);
        }
    }

    /**
     * 
     * @param typeStr
     * @param fieldName
     * @param validationResult
     */
    private void checkDomainTypeValid(String typeStr, String fieldName,
            ValidationResult validationResult) {
        if (validationResult.hasError()) {
            return;
        }
        DomainType domainType = null;
        DomainType[] types = DomainType.values();
        for (DomainType type : types) {
            if (type.getName().equals(typeStr)) {
                domainType = type;
            }
        }
        if (null == domainType) {
            validationResult.addError(UpdateValidationError
                    .build4008Error(fieldName));
        }
    }

}
