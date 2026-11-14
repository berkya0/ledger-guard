package com.berkaykomur.accountservice.mapper;

import com.berkaykomur.accountservice.dto.AccountRequest;
import com.berkaykomur.accountservice.dto.AccountResponse;
import com.berkaykomur.accountservice.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Account toEntity(AccountRequest request);

    AccountResponse toResponse(Account account);
}
