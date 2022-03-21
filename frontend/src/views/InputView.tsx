import { Button, CircularProgress } from '@mui/material';
import React from 'react';
import CodeEditor from '../components/CodeEditor';
import { Output } from '../mocks/output';
import { sendRequest } from '../service';
import BugReportIcon from '@mui/icons-material/BugReport';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';

interface Props {
  setOutputs: (output: Output[]) => void;
  text: string;
  setText: (text: string) => void;
}

function InputView(props: Props) {
  const { text, setText, setOutputs } = props;
  const [snackbarOpen, setSnackbarOpen] = React.useState(false);
  const [loading, setLoading] = React.useState(false);

  const getOutputs = async (text: string) => {
    try {
      setLoading(true);
      const outputs = await sendRequest(text);

      if (outputs.length) {
        setOutputs(outputs);
      } else {
        setSnackbarOpen(true);
      }

    } catch (e) {
      alert(e);
    } finally {
      setLoading(false);
    }
  }

  const readFile = (file: File) => {
    const reader = new FileReader();

    reader.onload = () => {
      if (reader.result) {
        setText(reader.result as string);
      }
    }

    reader.onerror = () => {
      console.log(reader.error);
    }

    reader.readAsText(file);
  }

  const handleClose = (_event?: React.SyntheticEvent | Event, reason?: string) => {
    if (reason !== 'clickaway') {
      setSnackbarOpen(false);
    }
  }

  return (
    <div style={{ width: '100vw', height: '100vh', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
      <p style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', fontSize: '20px' }}>
        <BugReportIcon />Bug-jet Printer
      </p>
      <CodeEditor
        text={text}
        setText={setText}
        readOnly={false}
        showPrintMargin={false}
        height="80%"
        width="100%"
        style={{ borderTop: '1px solid lightgrey', borderBottom: '1px solid lightgrey' }}
        markers={[]}
      />
      <div style={{ display: 'flex', flexDirection: 'row', margin: 'auto', gap: '100px' }}>
        <Button
          size="large"
          variant="contained"
          component="label"
        >
          Upload file
          <input type="file" accept=".java" hidden onChange={(e) => {
            if (e.target.files) {
              readFile(e.target.files[0]);
            }
          }} />
        </Button>
        <Button
          size="large"
          onClick={() => getOutputs(text)}
          variant="contained"
          color="success"
          disabled={loading}
          style={{ backgroundColor: '#2E7D32', width: 128, opacity: loading ? 0.7 : 1 }}
        >
          {loading ?
            (
              <CircularProgress
                size={28}
                style={{ color: 'white' }}
              />
            ) :
            "Send code"}
        </Button>
      </div>
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={5000}
        onClose={handleClose}
      >
        <Alert onClose={handleClose} severity="warning" sx={{ width: '100%' }}>
          No changes were logged!
        </Alert>
      </Snackbar>
    </div>
  )
}

export default InputView;
