import * as React from 'react';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListSubheader from '@mui/material/ListSubheader';
import Switch from '@mui/material/Switch';
import { Output, Scope } from '../mocks/output';
import Tooltip from '@mui/material/Tooltip';
import ListItemButton from '@mui/material/ListItemButton';

interface Props {
  slices: (Output & { show: boolean })[];
  toggleShowSlice: (name: string, scope: Scope) => void;
}

export default function SwitchList(props: Props) {

  const scrollToSliceByScope = (scope: Scope) => {
    document.getElementById(JSON.stringify(scope))?.scrollIntoView();
  }

  return (
    <List
      sx={{
        width: "100%", maxHeight: 250, bgcolor: "background.paper", margin: "auto",
        marginBottom: 3, borderRadius: 2, border: '1px solid black', boxSizing: 'border-box', padding: 0, overflow: 'auto'
      }}
      subheader={<ListSubheader style={{ borderTopRightRadius: 7, borderTopLeftRadius: 7, backgroundColor: '#1976d2', color: 'white', position: 'relative' }}>Variable Names:</ListSubheader>}
    >
      {props.slices.map(((slice, idx) => (
        <ListItem key={slice.name + idx} style={{ borderTop: '1px solid black' }}>
          <Tooltip title={slice.nickname} placement="bottom-start" arrow followCursor>
            <ListItemButton
              id={`switch-list-label-${slice.name + idx + slice.scope}`}
              onClick={() => scrollToSliceByScope(slice.scope)}
            >
              {`${slice.type} ${slice.name}`}
            </ListItemButton>
          </Tooltip>
          <Switch
            edge="end"
            onChange={() => props.toggleShowSlice(slice.name, slice.scope)}
            checked={slice.show}
            inputProps={{
              "aria-labelledby": "switch-list-label-variable-a"
            }}
          />
        </ListItem>
      )))}
    </List>
  );
}
