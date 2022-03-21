import * as React from 'react';
import Box from '@mui/material/Box';
// import CodeEditor from './CodeEditor';
import LineChart from './LineChart';
import LabelButton from './LabelButton';
import { Output } from '../mocks/output';
import { IMarker } from 'react-ace';
import BarChartSlider from './BarChartSlider';
import TwoDarraySlider from './2DarraySlider';
import Object from './Object';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';


interface Props {
  name: string;
  // text: string;
  marginBottom?: string;
  output: Output;
  setMarker: (arr: IMarker[]) => void;
  setExpanded: (slice: Output | null) => void;
  isExpanded: boolean;
}

export default function ProgramSlice(props: Props) {
  const { output, name, marginBottom, setMarker, setExpanded, isExpanded } = props;

  const getChart = () => {
    switch (output.type) {
      case "int[][]":
        return <TwoDarraySlider output={output} setMarker={setMarker} />;
      case "double[][]":
        return <TwoDarraySlider output={output} setMarker={setMarker} />;
      case "float[][]":
        return <TwoDarraySlider output={output} setMarker={setMarker} />;
      case "int[]":
        return <BarChartSlider output={output} setMarker={setMarker} />;
      case "char":
        return <TwoDarraySlider output={output} setMarker={setMarker} />;
      case "char[]":
          return <TwoDarraySlider output={output} setMarker={setMarker} />;
      case "int":
      case "short":
      case "long":
      case "double":
      case "float":
        return <LineChart output={output} setMarker={setMarker} />
      default:
        return <Object output={output} setMarker={setMarker} />;
    }
  }

  return (
    <Box
      sx={{
        width: "100%", height: isExpanded ? "99.8%" : 320, display: "flex",
        border: '1px solid black', marginBottom: marginBottom, borderRadius: "5px"
      }}
      id={JSON.stringify(output.scope)}
    >
      <LabelButton name={name} />
      {/* <CodeEditor text={props.text} readOnly={true} /> */}
      <div style={{ width: '100%' }}>
        <div style={{ height: 'calc(100% - 40px)', width: '100%', display: 'flex' }}>
          {getChart()}
          {/* <ExpandMoreIcon color="primary"/> */}
        </div>
        {isExpanded ? (
          <Tooltip title="Expand less">
            <IconButton onClick={() => setExpanded(null)}>
              <ExpandLessIcon />
            </IconButton>
          </Tooltip>
        ) : (
          <Tooltip title="Expand more">
            <IconButton onClick={() => setExpanded(output)}>
              <ExpandMoreIcon />
            </IconButton>
          </Tooltip>
        )}
      </div >
      {/* //<LineChart output={props.output} setMarker={props.setMarker} /> */}
    </Box >
  );
}
