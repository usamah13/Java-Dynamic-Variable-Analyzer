import React from 'react';
import './App.css';
import OutputView from './views/OutputView';
import InputView from './views/InputView';
import { Output } from './mocks/output';


function App() {
  const [text, setInput] = React.useState(sessionStorage.getItem("code") || "import annotation.Track;\n\n");
  const [output, setOutputs] = React.useState<Output[]>([]);

  const setText = (code: string) => {
    setInput(code);
    sessionStorage.setItem("code", code);
  }
  
  if (!output.length) {
    return <InputView text={text} setText={setText} setOutputs={setOutputs} />
  } else {
    return <OutputView program={text} output={output} />
  }
}

export default App;
