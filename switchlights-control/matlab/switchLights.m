function [ stateOut, durationOut ] = switchLights( stateIn, durationIn, qN, qE, qS, qW )
  if durationIn > 0
      durationOut = durationIn - 1;
      stateOut = stateIn;
  else
      durationOut = 30;
      if strcmp(stateIn, 'we') == 1
          stateOut = 'ns';
      else
          stateOut = 'we';
      end
  end;

end
