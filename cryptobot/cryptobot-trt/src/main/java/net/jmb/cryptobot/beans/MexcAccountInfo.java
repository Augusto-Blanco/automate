package net.jmb.cryptobot.beans;

import java.util.List;

public class MexcAccountInfo {
	
	String accountType;
	boolean canTrade;
	boolean canWithdraw;
	boolean canDeposit;
	List<MexcAsset> balances;
	
	
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public boolean isCanTrade() {
		return canTrade;
	}
	public void setCanTrade(boolean canTrade) {
		this.canTrade = canTrade;
	}
	public boolean isCanWithdraw() {
		return canWithdraw;
	}
	public void setCanWithdraw(boolean canWithdraw) {
		this.canWithdraw = canWithdraw;
	}
	public boolean isCanDeposit() {
		return canDeposit;
	}
	public void setCanDeposit(boolean canDeposit) {
		this.canDeposit = canDeposit;
	}
	public List<MexcAsset> getBalances() {
		return balances;
	}
	public void setBalances(List<MexcAsset> balances) {
		this.balances = balances;
	}
	public Double getFreeAssetQuantity(String symbol) {
		if (balances != null) {
			return balances.stream()
				.filter(asset -> symbol.equalsIgnoreCase(asset.getAsset()))
				.map(MexcAsset::getFree)
				.findFirst()
				.orElse(0d);
		}
		return 0d;
	}
	
	@Override
	public String toString() {
		return "MexcAccountInfo [accountType=" + accountType + ", canTrade=" + canTrade + ", canWithdraw=" + canWithdraw
				+ ", canDeposit=" + canDeposit + ", balances=" + balances + "]";
	}
	

}
