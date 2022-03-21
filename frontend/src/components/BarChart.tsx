import React from "react";
import {
  BarChart as ReBarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Legend
} from "recharts";
import { Output } from '../mocks/output';

interface Props {
  output: Output;
  index: number;
}

interface State {
  color: string;
}


function getRandomColor() {
  const r = Math.floor(Math.random() * 200);
  const g = Math.floor(Math.random() * 200);
  const b = Math.floor(Math.random() * 200);
  const color = "rgb(" + r + "," + g + "," + b + ")";

  return color;
}

export default class BarChart extends React.PureComponent<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      color: getRandomColor()
    }
  }

  render() {
    const entry = this.props.output.history[this.props.index].value;
    const parsedArr = JSON.parse(entry);

    const data = typeof parsedArr !== "object" || parsedArr === null ?
      [0]
      : parsedArr.map((e: number, idx: number) => {
        return {
          index: idx,
          value: e,
        }
      });


    return (
      <ReBarChart
        width={500}
        height={300}
        style={{ margin: 'auto' }}
        data={data}
        margin={{

          top: 20,
          right: 30,
          left: 20,
          bottom: 5
        }}
      >
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="index" />
        <YAxis yAxisId="left" orientation="left" stroke="#8884d8" />
        <YAxis yAxisId="right" orientation="right" stroke="#82ca9d" />
        <Legend />
        <Bar yAxisId="left" dataKey="value" fill={this.state.color} />
      </ReBarChart>
    );
  }
}
