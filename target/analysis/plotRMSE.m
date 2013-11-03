function plotRMSE(data)

disp('plotting RMSE:');

globalMean = mean([data(1,10),data(4,10),data(10,10),data(13,10),data(17,10)]);
popular = mean([data(2,10),data(6,10),data(10,10),data(15,10),data(18,10)]);
itemMean = mean([data(3,10),data(7,10),data(11,10),data(14,10),data(19,10)]);
persMean = mean([data(5,10),data(9,10),data(12,10),data(16,10),data(20,10)]);

bars = [popular, persMean, itemMean]
labels = {'popular'; 'persMean'; 'itemMean'};
bar(bars, 0.4, 'r');
set(gca, 'XTickLabel',labels, 'XTick',1:numel(labels));
