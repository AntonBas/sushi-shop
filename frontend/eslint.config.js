import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import jsxA11y from 'eslint-plugin-jsx-a11y-x'
import tseslint from 'typescript-eslint'
import { defineConfig, globalIgnores } from 'eslint/config'

const DIRECTIVE_COMMENT = /^\s*(eslint-disable|eslint-enable|eslint-disable-line|eslint-disable-next-line)(\s|$)/
const TS_DIRECTIVE_COMMENT = /^\s*@ts-expect-error(\s|$)/
const TRIPLE_SLASH_REFERENCE = /^\/\s*<reference\s/

function isAllowedComment(comment) {
  if (comment.type === 'Block') {
    return DIRECTIVE_COMMENT.test(comment.value)
  }
  return DIRECTIVE_COMMENT.test(comment.value)
    || TS_DIRECTIVE_COMMENT.test(comment.value)
    || TRIPLE_SLASH_REFERENCE.test(comment.value)
}

const noCodeComments = {
  meta: {
    type: 'suggestion',
    messages: { noComment: 'Code comments are not allowed' },
    schema: [],
  },
  create(context) {
    return {
      Program() {
        for (const comment of context.sourceCode.getAllComments()) {
          if (!isAllowedComment(comment)) {
            context.report({ loc: comment.loc, messageId: 'noComment' })
          }
        }
      },
    }
  },
}

export default defineConfig([
  globalIgnores(['dist', 'coverage']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      tseslint.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
      jsxA11y.configs.recommended,
    ],
    plugins: {
      project: { rules: { 'no-code-comments': noCodeComments } },
    },
    languageOptions: {
      globals: globals.browser,
      parserOptions: {
        projectService: true,
        tsconfigRootDir: import.meta.dirname,
      },
    },
    rules: {
      'no-console': 'error',
      'project/no-code-comments': 'error',
      '@typescript-eslint/no-explicit-any': 'error',
      '@typescript-eslint/no-unsafe-argument': 'error',
      '@typescript-eslint/no-unsafe-assignment': 'error',
      '@typescript-eslint/no-unsafe-call': 'error',
      '@typescript-eslint/no-unsafe-member-access': 'error',
      '@typescript-eslint/no-unsafe-return': 'error',
      '@typescript-eslint/no-floating-promises': 'error',
      '@typescript-eslint/no-misused-promises': 'error',
    },
  },
])
