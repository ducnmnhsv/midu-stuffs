module.exports = {
    env: {
        browser: true,
        node: true,
        es6: true,
        es2021: true
    },
    extends: [
        'plugin:prettier/recommended',
        'prettier',
        'eslint:recommended'
    ],
    parser: '@typescript-eslint/parser',
    plugins: ['@typescript-eslint'],
    overrides: [
        {
            "env": {
                "node": true
            },
            "files": [
                ".eslintrc.{js,cjs}"
            ],
            "parserOptions": {
                "sourceType": "module"
            }
        }
    ],
    "parserOptions": {
        "ecmaVersion": "latest",
        "sourceType": "module",
        "project": 'tsconfig.json'
    },
    rules: {
        // 'no-var': 'error',
        // semi: 'error',
        // indent: ['error', 2, { SwitchCase: 1 }],
        // 'no-multi-spaces': 'error',
        // 'space-in-parens': 'error',
        // 'no-multiple-empty-lines': 'error',
        // 'prefer-const': 'error',
    }
}
