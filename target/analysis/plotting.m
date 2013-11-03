clear all;

%reading data
evalResults=importdata('eval-results.csv');
data = evalResults.data;

%plotting data
figure;
plotRMSE(data);
figure;
plotnDCG(data);
