import React, { useState } from 'react';
import { Button, FormGroup, Label, Input, Alert } from 'reactstrap';
import axios from 'axios';

const CreateMarketHistoryJobResult = () => {
  const [inputValue, setInputValue] = useState('');
  const [response, setResponse] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInputValue(e.target.value);
  };

  const handleSubmit = async () => {
    if(!inputValue.trim()){
      setValidationError("Input symbols are required.");
      return;
    }
    const symbols = inputValue.split(',').map(symbol => symbol.trim());
    try {
      const result = await axios.post('/api/create-market-history', { symbols },
        { timeout: 570000 });
      setResponse(`Success: ${JSON.stringify(result.data)}`);
      setError(null);
      setLoading(false);
      window.location.reload();
    } catch (err) {
      setError("An error occurred while processing your request. Please try again later.");
      setResponse(null);
      setLoading(false);
    }
  };

  return (
    <div>
      <h2>Create Market History Job Result</h2>
      <FormGroup>
        <Label for="marketHistoryInput">Input Symbols</Label>
        <Input
          type="text"
          name="input"
          id="marketHistoryInput"
          value={inputValue}
          onChange={handleInputChange}
          placeholder="Enter symbols separated by commas"
        />
        {validationError && <Alert color="warning">{validationError}</Alert>}
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
