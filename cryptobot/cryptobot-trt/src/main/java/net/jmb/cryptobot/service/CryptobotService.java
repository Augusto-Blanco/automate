package net.jmb.cryptobot.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import net.jmb.cryptobot.data.bean.OrderQO;
import net.jmb.cryptobot.data.bean.PageData;
import net.jmb.cryptobot.data.entity.Trade;
import net.jmb.cryptobot.data.repository.CryptobotRepository;
import net.jmb.cryptobot.enums.ParamContext;

@Service
public class CryptobotService {
	
	public static final ParamContext CONTEXTE = ParamContext.TOUT_CONTEXTE;

	@Autowired
	protected CryptobotRepository cryptobotRepository;
	
	
	
	
	public List<Trade> getTradesForAsset(Long assetId) {
		return cryptobotRepository.getTradesForAsset(assetId);
	}

	
	public PageData<Trade> getTrades(Pageable pageRequest) {
		return getTrades((OrderQO) null, pageRequest);
	}

	public PageData<Trade> getTrades(OrderQO orderQO, Pageable pageRequest) {
		return cryptobotRepository.getTrades(orderQO, pageRequest);
	}
	
	public Trade getTradeByRef(String tradeRef) {
		OrderQO orderQO = new OrderQO().tradeRef(tradeRef);
		PageData<Trade> trades = cryptobotRepository.getTrades(orderQO, null);
		if (trades != null && !trades.isEmpty()) {
			return trades.getData().get(0);
		}
		return null;
	}
	

	public CryptobotRepository getCryptobotRepository() {
		return cryptobotRepository;
	}
	







	


}
