import React from 'react';
import { IMarker } from 'react-ace/lib/types';
import { Output } from '../mocks/output';
import BarChart from './BarChart';
import Box from '@mui/material/Box';
import Slider from '@mui/material/Slider';


interface Props {
  output: Output;
  setMarker: (arr: IMarker[]) => void;
}

export default function BarchartSlider(props: Props) {
  const { output, setMarker } = props;
  const [sliderValue, setSliderValue] = React.useState(0);

  const getEntryInfo = (value: number) => {
    const entry = output.history[value];
    return (
      <div className="custom-tooltip" style={{ backgroundColor: 'lightgrey', paddingLeft: 10, paddingRight: 10 }}>
        <p className="line-number">Line number: {entry.line}</p>
        <p className="value">Value: {entry.value}</p>
        <p className="enclosing-class">Class: {entry.enclosingClass}</p>
        <p className="enclosing-method">Method: {entry.enclosingMethod}</p>
      </div>
    )
  }

  const setSlider = (value: number) => {
    const entry = output.history[value];
    setSliderValue(value);
    setMarker([{
      startRow: entry.line - 1,
      endRow: entry.line,
      startCol: 0,
      endCol: 0,
      className: 'replacement_marker',
      type: 'text'
    }]);
  }

  const marks = output.history.map((e, idx) => {
    return {
      value: idx
    }
  })

  return (
    <div style={{ display: 'flex' }}>
      <BarChart output={output} index={sliderValue} />
      <Box sx={{ width: 300, display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
        <Slider
          aria-label="Array Values"
          value={sliderValue}
          onChange={(_e, val) => setSlider(val as number)}
          valueLabelDisplay="auto"
          step={1}
          max={output.history.length - 1}
          marks={marks}
        />
        {getEntryInfo(sliderValue)}
      </Box>
    </div>
  )
}