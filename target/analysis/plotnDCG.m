function plotnDCG(data)

disp('plotting nDCG evaluaton at 10:');

globalMean = mean([data(1,8),data(4,8),data(8,8),data(13,8),data(17,8)]);
popular = mean([data(2,8),data(6,8),data(10,8),data(15,8),data(18,8)]);
itemMean = mean([data(3,8),data(7,8),data(11,8),data(14,8),data(19,8)]);
persMean = mean([data(5,8),data(9,8),data(12,8),data(16,8),data(20,8)]);

bars = [persMean, itemMean, globalMean]
labels = {'persMean'; 'itemMean'; 'globalMean'};
bar(bars, 0.4, 'r');
set(gca, 'XTickLabel',labels, 'XTick',1:numel(labels));
