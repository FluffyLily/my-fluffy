export const validatePassword = async (password) => {
    const minLength = 8;
    const hasUpperCase = /[A-Z]/.test(password);
    const hasLowerCase = /[a-z]/.test(password);
    const hasDigits = /\d/.test(password);
    const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password);
  
    if (password.length < minLength) {
      return "비밀번호는 최소 8자 이상이어야 합니다.";
    } else if (!hasLowerCase) {
      return "비밀번호는 소문자를 하나 이상 포함해야 합니다.";
    } else if (!hasDigits) {
      return "비밀번호는 숫자를 하나 이상 포함해야 합니다.";
    } else if (!hasSpecialChar) {
      return "비밀번호는 특수문자를 하나 이상 포함해야 합니다.";
    } else {
      return '';
    }
  };