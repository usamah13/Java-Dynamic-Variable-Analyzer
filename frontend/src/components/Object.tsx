import React from 'react';
import { IMarker } from 'react-ace/lib/types';
import { Output } from '../mocks/output';
import Box from '@mui/material/Box';
import Slider from '@mui/material/Slider';
import ReactJson from 'react-json-view';


interface Props {
  output: Output;
  setMarker: (arr: IMarker[]) => void;
}

export default function ObjectSlider(props: Props) {
  const { output, setMarker } = props;
  const [sliderValue, setSliderValue] = React.useState(0);

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

  const outputJSON = () => {
    return {
      ...output.history[sliderValue],
      value: JSON.parse(output.history[sliderValue].value),
    }
  }

  const marks = output.history.map((e, idx) => {
    return {
      value: idx
    }
  });

  return (
    <div style={{ display: 'flex', width: '100%' }}>
      <Box sx={{ width: '90%', margin: 'auto', height: '100%' }}>
        <Slider
          aria-label="Array Values"
          value={sliderValue}
          onChange={(e, val) => setSlider(val as number)}
          valueLabelDisplay="auto"
          step={1}
          max={output.history.length - 1}
          marks={marks}
        />
        <ReactJson style={{ textAlign: 'left', maxHeight: '85%', overflow: 'auto' }} src={outputJSON()} />
      </Box>
    </div>
  )
}