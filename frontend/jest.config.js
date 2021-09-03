module.exports = {
  preset: "ts-jest",
  testEnvironment: "jsdom",
  moduleFileExtensions: ["js", "jsx", "json", "tsx", "ts"],
  transform: {
    ".+\\.(css|styl|less|sass|scss|png|jpg|ttf|woff|woff2)$": "babel-jest",
    "^.+\\.js$": "babel-jest",
    "^.+\\.jsx$": "babel-jest",
    "^.+\\.tsx$": "ts-jest",
    "^.+\\.ts$": "ts-jest",
  },
  moduleNameMapper: {
    "\\.(css|less|sass|scss)$": "<rootDir>/__mocks__/styleMock.js",
    "\\.(gif|ttf|eot|svg)$": "<rootDir>/__mocks__/fileMock.js",
  },
  transformIgnorePatterns: ["<rootDir>/node_modules"],
  watchPathIgnorePatterns: ["<rootDir>/dist", "<rootDir>/node_modules"],
  setupFilesAfterEnv: [
    "<rootDir>/__mocks__/consoleMock.js",
    "<rootDir>/__mocks__/setup.js",
  ],
};
