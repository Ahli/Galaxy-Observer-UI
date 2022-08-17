import { render, screen } from '@testing-library/react';
import App from './App';

test('renders', () => {
  render(<App />);
  const elements = screen.getAllByText('News');
  expect(elements).toHaveLength(2);
  expect(elements[0]).toBeInTheDocument();
  expect(elements[1]).toBeInTheDocument();
});
