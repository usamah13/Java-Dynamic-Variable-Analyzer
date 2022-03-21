import * as React from 'react';
import Button from '@mui/material/Button';

interface Props {
  name: string;
}

export default function BasicButtons(props: Props) {
  return (
    <Button
      variant="contained"
      disableElevation
      disableRipple
      style={{ height: '100.2%', cursor: 'inherit', backgroundColor: '#1976d2' }}>
      {props.name}
    </Button>
  );
}