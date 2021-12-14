import React, { useState } from 'react';
import FormControl from '@mui/material/FormControl';
import './App.css';
import { Button, Card, CardActions, CardContent, Grid, Typography } from '@mui/material';
function App() {
  const [filename, setFilename] = useState("No selected");

  const onChangeSelectFile = (a: React.ChangeEvent<HTMLInputElement>) => {
    if (a.target.files?.length ?? 0 >= 1) {
      setFilename(a.target.files![0].name)
    } else {
      setFilename("No selected")
    }
  }

  return (
    <Grid container justifyContent="center" alignItems="center" spacing={1}>
      <Grid item xs={12}>
        <Typography variant="h2" align="center">
          shttpa
        </Typography>
      </Grid>
      <Grid item xs={10} sm="auto">
        <FormControl component="form" method="post" encType="multipart/form-data" acceptCharset="UTF-8" sx={{ width: { xs: "100%", sm: 534 } }}>
          <Card>
            <CardContent>
              <Grid
                container
                direction="row"
                justifyContent="center"
                alignItems="center"
                spacing={2}
              >
                <Grid item xs={6} sm={8}>
                  <Typography variant="body1">{filename}</Typography>
                </Grid>
                <Grid item xs="auto" sm={4}>
                  <Button component="label" sx={{ my: 2 }}>
                    Select File
                    <input name="file" type="file" hidden onChange={onChangeSelectFile} />
                  </Button>
                </Grid>
              </Grid>
            </CardContent>
            <CardActions>
              <Button size="small" type="submit">Send</Button>
            </CardActions>
          </Card>
        </FormControl>
      </Grid>
    </Grid >
  );
}

export default App;
