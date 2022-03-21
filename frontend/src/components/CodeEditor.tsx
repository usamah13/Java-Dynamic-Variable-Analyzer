import React from "react";
import AceEditor, { IMarker } from "react-ace";

import "ace-builds/src-noconflict/mode-java";

interface Props {
  text: string;
  setText?: (text: string) => void;
  readOnly?: boolean;
  height?: string;
  width?: string;
  showPrintMargin?: boolean;
  markers: IMarker[];
  style?: React.CSSProperties;
}

function CodeEditor(props: Props) {
  const editorRef = React.useRef<AceEditor>(null);

  const onChange = (newValue: string) => {
    if (props.setText) {
      props.setText(newValue);
    }
  }

  React.useEffect(() => {
    if (props.markers.length > 0) {
      editorRef.current?.editor.scrollToLine(props.markers[0].startRow, true, true, () => {});
    }
  }, [props.markers]);


  return (
    <AceEditor
      ref={editorRef}
      placeholder="Write your code here"
      mode="java"
      name="hello"
      height={props.height || "100%"}
      width={props.width}
      style={props.style}
      readOnly={props.readOnly}
      value={props.text}
      onChange={onChange}
      fontSize={14}
      highlightActiveLine={true}
      markers={props.markers}
      showPrintMargin={props.showPrintMargin !== false}
      setOptions={{
        // enableBasicAutocompletion: false,
        // enableLiveAutocompletion: true,
        showLineNumbers: true,
        tabSize: 2,
      }} />

  );
}

export default CodeEditor;
