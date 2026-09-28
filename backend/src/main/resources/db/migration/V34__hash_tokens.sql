UPDATE tokens
SET token = encode(sha256(convert_to(token, 'UTF8')), 'hex');
