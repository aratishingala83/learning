function validatePositiveNumber(input) {
  // Convert input to string to handle both numbers and string inputs
  const value = input.toString().trim();

  // Regular expression to match positive numbers with up to three decimal places
  const regex = /^(\d+)(\.\d{1,3})?$/;

  // Test the input against the regular expression
  if (regex.test(value)) {
    return true; // Valid positive number with up to three decimal precision
  }

  return false; // Not valid
}
