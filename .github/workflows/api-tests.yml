name: Explore With Me Tests

on:
  workflow_dispatch:
    inputs:
      stats_test_mode:
        description: "Режим тестирования рекомендательной системы"
        required: false
        default: ANALYZE
        type: choice
        options:
          - COLLECTION
          - AGGREGATION
          - ANALYZE
      verbose_mode:
        description: "Включить подробные логи во время тестов"
        required: false
        default: false
        type: boolean
      print_logs:
        description: "Включить вывод логов в консоль"
        required: false
        default: false
        type: boolean
  pull_request:

jobs:
  build:
    uses: yandex-praktikum/java-plus-graduation/.github/workflows/api-tests.yml@ci
    with:
      stats_test_mode: ${{ inputs.stats_test_mode }}
      verbose_mode: ${{ inputs.verbose_mode }}
      print_logs: ${{ inputs.print_logs }}