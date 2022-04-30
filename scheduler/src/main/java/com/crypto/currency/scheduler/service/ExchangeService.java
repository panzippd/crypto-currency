package com.crypto.currency.scheduler.service;

import com.crypto.currency.common.utils.CollectionUtils;
import com.crypto.currency.common.utils.StringUtils;
import com.crypto.currency.data.entity.ExchangeEntity;
import com.crypto.currency.data.entity.MarketEntity;
import com.crypto.currency.data.repository.mongo.cryptocurrency.ExchangeRepository;
import com.crypto.currency.data.repository.mongo.cryptocurrency.MarketRepository;
import com.crypto.currency.scheduler.model.ExchangeInfoDTO;
import com.crypto.currency.scheduler.model.MarketPairDTO;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Panzi
 * @Description processing exchange data
 * @date 2022/4/29 19:00
 */
@Service
public class ExchangeService {

    @Autowired
    private ExchangeRepository exchangeRepository;
    @Autowired
    private MarketRepository marketRepository;

    /**
     * get all exchange data
     *
     * @return
     */
    public List<ExchangeInfoDTO> getExchanges() {
        return Mono.zip(exchangeRepository.findByIsActive(true).collectList(),
            marketRepository.findByIsActive(true).collectList()).map(r -> this.reduce(r.getT1(), r.getT2())).block();
    }

    private List<ExchangeInfoDTO> reduce(List<ExchangeEntity> exchangeEntities, List<MarketEntity> marketEntities) {
        Map<Integer, List<MarketPairDTO>> marketMap = marketEntities.stream().collect(
            Collectors.groupingBy(MarketEntity::getExchangeId,
                Collectors.mapping(this::getMarketPairDTO, Collectors.toList())));

        List<ExchangeInfoDTO> exchangeList = Lists.newArrayListWithExpectedSize(exchangeEntities.size());
        for (ExchangeEntity entity : exchangeEntities) {
            List<MarketPairDTO> marketDTOs = marketMap.get(entity.getExchangeId());
            if (CollectionUtils.isEmpty(marketDTOs)) {
                continue;
            }
            exchangeList.add(getExchangeInfoDTO(entity, marketDTOs));
        }
        return exchangeList;
    }

    private MarketPairDTO getMarketPairDTO(MarketEntity entity) {
        MarketPairDTO marketPairDTO = new MarketPairDTO();
        marketPairDTO.setBaseId(entity.getBaseCryptoCurrencyId());
        marketPairDTO.setCategory(entity.getCategory());
        marketPairDTO.setMainId(entity.getMainCryptoCurrencyId());
        String baseSymbol = StringUtils.getIfBlank(entity.getBaseSymbolOverride(), entity::getBaseSymbol);
        String mainSymbol = StringUtils.getIfBlank(entity.getMainSymbolOverride(), entity::getMainSymbol);
        marketPairDTO.setMainSymbol(mainSymbol);
        marketPairDTO.setBaseSymbol(baseSymbol);
        return marketPairDTO;
    }

    private ExchangeInfoDTO getExchangeInfoDTO(ExchangeEntity entity, List<MarketPairDTO> marketDTOs) {
        ExchangeInfoDTO dto = new ExchangeInfoDTO();
        dto.setExchangeId(entity.getExchangeId());
        dto.setExchangeName(entity.getName());
        dto.setPairs(marketDTOs);
        return dto;
    }
}
