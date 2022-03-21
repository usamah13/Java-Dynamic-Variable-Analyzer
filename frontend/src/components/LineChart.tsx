import React, { PureComponent } from 'react';
import { Output } from '../mocks/output';
import {
  ScatterChart,
  Scatter,
  XAxis,
  YAxis,
  ZAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { IMarker } from 'react-ace';
import { SymbolType } from 'recharts/types/util/types';


const symbols: SymbolType[] = ['circle', 'cross', 'diamond', 'square', 'star', 'triangle', 'wye'];

interface Props {
  output: Output;
  setMarker: (arr: IMarker[]) => void;
}

interface State {
  symbol: SymbolType;
  color: string;
}

function getRandomColor() {
  const r = Math.floor(Math.random() * 200);
  const g = Math.floor(Math.random() * 200);
  const b = Math.floor(Math.random() * 200);
  const color = "rgb(" + r + "," + g + "," + b + ")";

  return color;
}

export default class LineChart extends PureComponent<Props, State> {
  static demoUrl = 'https://codesandbox.io/s/scatter-chart-with-joint-line-2ucid';
  constructor(props: Props) {
    super(props);

    this.state = {
      symbol: symbols[Math.floor(Math.random() * symbols.length)],
      color: getRandomColor(),
    };
  }

  CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      const line = payload[0].payload.line as number;
      this.props.setMarker([{
        startRow: line - 1,
        endRow: line,
        startCol: 0,
        endCol: 0,
        className: 'replacement_marker',
        type: 'text'
      }]);

      return (
        <div className="custom-tooltip" style={{ backgroundColor: 'lightgrey', paddingLeft: 10, paddingRight: 10 }}>
          <p className="line-number">Line number: {line}</p>
          <p className="value">Value: {payload[1].payload.value}</p>
          <p className="enclosing-class">Class: {payload[0].payload.enclosingClass}</p>
          <p className="enclosing-method">Method: {payload[0].payload.enclosingMethod}</p>
        </div>
      );
    }

    this.props.setMarker([]);
    return null;
  }

  render() {
    const axis2 = this.props.output.history.map((e, idx) => {
      return {
        x: idx * 2,
        y: JSON.parse(e.value),
        ...e,
      }
    });

    return (
      <ResponsiveContainer width="90%" height="100%">
        <ScatterChart
          width={500}
          height={400}
          margin={{
            top: 20,
            right: 20,
            bottom: 20,
            left: 40,
          }}
        >
          <CartesianGrid />
          <XAxis type="number" dataKey="x" name="line number" hide />
          {/* Maybe want to use unit as double later? */}
          <YAxis type="number" dataKey="y" name="Value" />
          <ZAxis type="number" range={[100]} />
          <Tooltip cursor={{ strokeDasharray: '3 3' }} content={this.CustomTooltip} />
          <Legend />
          <Scatter name={`${this.props.output.type} ${this.props.output.name}`} data={axis2} fill={this.state.color} line shape={this.state.symbol} />
        </ScatterChart>
      </ResponsiveContainer>
    );
  }
}
