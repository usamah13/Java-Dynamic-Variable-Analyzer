import { Output } from '../mocks/output';

export const sendRequest = async (program: string): Promise<Output[]> => {
  return fetch('http://localhost:8080/output', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ program })
  }).then(async res => {
    if (!res.ok) {
      throw new Error(await res.text());
    }
    return res.json() as unknown as Output[];
  });
}
