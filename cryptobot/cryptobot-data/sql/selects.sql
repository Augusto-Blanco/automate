select symbol, `datetime` date, currentSide side, flagBuy buy, flagSell sell, nbLoss, percentLoss pctLoss, 
amountB100 amount, price, buyPrice, sellPrice, bestBuyPrice bestBuy, prevBestBuyPrice prevBestBuy, 
bestSellPrice bestSell, canResetBestSellPrice canResetSell, canResetBestBuyPrice canResetBuy
from cotation c where symbol = 'BTC' and `datetime` >= '2025-02-14 16:20:00';

select symbol, `datetime` date, currentSide side, flagBuy buy, flagSell sell, nbLoss, percentLoss pctLoss, 
amountB100 amount, price, buyPrice, sellPrice, bestBuyPrice bestBuy, prevBestBuyPrice prevBestBuy, 
bestSellPrice bestSell, canResetBestSellPrice canResetSell, canResetBestBuyPrice canResetBuy
from cotation c where symbol = 'CYBRO' and `datetime` >= '2025-02-14 20:45:00';

select symbol, `datetime` date, currentSide side, flagBuy buy, flagSell sell, nbLoss, percentLoss pctLoss, 
amountB100 amount, price, buyPrice, sellPrice, bestBuyPrice bestBuy, prevBestBuyPrice prevBestBuy, 
bestSellPrice bestSell, canResetBestSellPrice canResetSell, canResetBestBuyPrice canResetBuy
from cotation c where symbol = 'LINK' and `datetime` >= '2025-02-14 13:45:00';

select symbol, `datetime` date, currentSide side, flagBuy buy, flagSell sell, nbLoss, percentLoss pctLoss, 
amountB100 amount, price, buyPrice, sellPrice, bestBuyPrice bestBuy, prevBestBuyPrice prevBestBuy, 
bestSellPrice bestSell, canResetBestSellPrice canResetSell, canResetBestBuyPrice canResetBuy
from cotation c where symbol = 'DEEPSEEK' and `datetime` >= '2025-02-14 05:35:00';

select symbol, `datetime` date, currentSide side, flagBuy buy, flagSell sell, nbLoss, percentLoss pctLoss, 
amountB100 amount, price, buyPrice, sellPrice, bestBuyPrice bestBuy, prevBestBuyPrice prevBestBuy, 
bestSellPrice bestSell, canResetBestSellPrice canResetSell, canResetBestBuyPrice canResetBuy
from cotation c where symbol = 'ADA' and `datetime` >= '2025-02-14 07:40:00';





update  cotation 
set amountB100 = null, nbLoss = null, currentSide = null, flagBuy =null , flagSell = null, buyPrice = null, bestBuyPrice = null, 
prevBestBuyPrice = null, sellPrice = null, bestSellPrice = null, canResetBestBuyPrice = null, canResetBestSellPrice Price = null
where symbol = 'BTC' and `datetime` >= '2025-02-03 15:35:00';

delete from asset_config where symbol = 'BTC' and endTime >= '2025-02-03 13:50:00';

