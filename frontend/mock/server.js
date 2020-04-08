const express = require('express');
const bodyParser = require('body-parser');


const app = express();
const port = 3000;

app.use(bodyParser.json());

app.use((_, res, next) => {
  res.header('');
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Headers', '*');
  res.header('Access-Control-Allow-Methods', '*');
  next();
});

app.get('/person/inntekt', (_, res) => {
  res.send(JSON.stringify({ beregnetMånedsinntekt: 45000 }));
});

app.get('/person/aktorid', (_, res) => {
  res.send(JSON.stringify(12345678910));
});

app.delete('/person', (req, res) => {
  console.log('Sletter person med fødselsnummer', req.header('ident'));
  res.status(204).send();
});

app.post('/vedtaksperiode', (req, res) => {
  console.log('Oppretter vedtaksperiode med data:', req.body);
  res.status(204).send();
});

app.post('/behov', (req, res) => {
  console.log('Oppretter behov med data:', req.body);
  res.status(204).send();
});

app.listen(port, () => console.log(`Mock-server kjører på port ${port}`));
