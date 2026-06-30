import React, { useState } from 'react';
import { Button, FormGroup, Label, Input, Alert } from 'reactstrap';
import axios from 'axios';

const CreateMarketHistoryJobResult = () => {
  const [inputValue, setInputValue] = useState('');
  const [response, setResponse] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInputValue(e.target.value);
  };

  const handleSubmit = async () => {
    try {
      const result = await axios.post('/admin/create-market-history', { data: inputValue });
      setResponse(`Success: ${JSON.stringify(result.data)}`);
      setError(null);
    } catch (err) {
      setError(`Error: ${err.response?.data?.message || err.message}`);
      setResponse(null);
    }
  };

  return (
    <div>
      <h2>Create Market History Job Result</h2>
      <FormGroup>
        <Label for="marketHistoryInput">Input Data</Label>
        <Input
          type="text"
          name="input"
          id="marketHistoryInput"
          value={inputValue}
          onChange={handleInputChange}
        />
      </FormGroup>
      <Button color="primary" onClick={handleSubmit}>
        Submit
      </Button>
      {response && <Alert color="success">{response}</Alert>}
      {error && <Alert color="danger">{error}</Alert>}
    </div>
  );
};

export default CreateMarketHistoryJobResult;
