import React from 'react';
import SwitchList from '../components/SwitchList';
import ProgramSlice from '../components/ProgramSlice';
import CodeEditor from '../components/CodeEditor';
import { IMarker } from 'react-ace';
import { Output, Scope } from '../mocks/output';

interface Props {
  program: string;
  output: Output[];
}

function OutputView(props: Props) {
  const { program, output } = props;

  const [slices, setShowSlices] = React.useState(output.map(e => ({
    ...e,
    show: true,
  })));
  const [marker, setMarker] = React.useState<IMarker[]>([]);
  const [expanded, setExpanded] = React.useState<Output | null>(null);

  const toggleShowSlice = (name: string, scope: Scope) => {
    setShowSlices(slices.map(e => {
      return (e.name === name && e.scope === scope) ? {
        ...e,
        show: !e.show
      } : e;
    }));
  }

  if (expanded) {
    return (
      <div className="App" style={{ margin: 40, display: "flex", justifyContent: "space-between" }}>
        <div style={{ marginRight: 20, height: 'calc(100vh - 80px)', minHeight: 600, position: 'sticky', top: 40 }}>
          <CodeEditor
            text={program}
            readOnly={true}
            height="100%"
            style={{ border: '1px solid black', borderRadius: 8 }}
            markers={marker}
          />
        </div>
        <div style={{ width: "100%" }}>
          <ProgramSlice
            name={expanded.name}
            output={expanded}
            setMarker={setMarker}
            setExpanded={setExpanded}
            isExpanded={true}
          />
        </div>
      </div>
    );
  }

  return (
    <div className="App" style={{ margin: 40, display: "flex", justifyContent: "space-between" }}>
      <div style={{ marginRight: 20, height: 'calc(100vh - 80px)', minHeight: 600, position: 'sticky', top: 40 }}>
        <SwitchList slices={slices} toggleShowSlice={toggleShowSlice} />
        <CodeEditor
          text={program}
          readOnly={true}
          height="calc(100% - 205px - 50px)"
          style={{ border: '1px solid black', borderRadius: 8 }}
          markers={marker}
        />
      </div>
      <div style={{ width: "100%" }}>
        {slices.filter(e => e.show).map((slice, idx) => {
          return (
            <ProgramSlice
              key={slice.name + idx}
              name={slice.name}
              output={slice}
              marginBottom={idx === slices.length - 1 ? "0" : "20px"}
              setMarker={setMarker}
              setExpanded={setExpanded}
              isExpanded={false}
            />
          )
        })}
      </div>
    </div>
  )
}

export default OutputView;
