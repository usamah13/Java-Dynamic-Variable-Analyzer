import React from 'react';
import { IMarker } from 'react-ace/lib/types';
import { Output } from '../mocks/output';
import Box from '@mui/material/Box';
import Slider from '@mui/material/Slider';


interface Props {
  output: Output;
  setMarker: (arr: IMarker[]) => void;
}

export default function TwoDarraySlider(props: Props) {
  const { output, setMarker } = props;
  const [sliderValue, setSliderValue] = React.useState(0);

  function getEntryInfo(value: number) {
    const entry = props.output.history[value];
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

  const marks = props.output.history.map((e, idx) => {
    return {
      value: idx
    }
  })

  return (
    <div style={{ display: 'flex', width: '100%' }}>
      {/* <BarChart output={props.output} setMarker={props.setMarker} /> */}
      <Box sx={{ width: '90%', margin: 'auto' }}>
        <Slider
          aria-label="Array Values"
          value={sliderValue}
          onChange={(e, val) => setSlider(val as number)}
          valueLabelDisplay="auto"
          step={1}
          max={props.output.history.length - 1}
          marks={marks}
        />
        {getEntryInfo(sliderValue)}
      </Box>
    </div>
  )
}