function [ cycleTime ] = getCycleTime( q, cycle, phaseTime, greenMatrix, mfParams, mfCounts, fRules, modelParams, findAlg )
%GETCYCLETIME Summary of this function goes here
%   Detailed explanation goes here
    fprintf('q: % i\n', q);
    fprintf('cycle: %i\n', cycle);
    fprintf('phaseTime: %i\n', phaseTime);
    fprintf('greenMatrix: % i\n', greenMatrix);
    fprintf('mfParams: %i\n', mfParams);
    fprintf('mfCounts: %i\n', mfCounts);
    fprintf('fRules: %i\n', fRules);
    fprintf('modelParams: %i\n', modelParams);
    fprintf('findAlg: %s\n', findAlg);
    cycleTime = q(2);
end