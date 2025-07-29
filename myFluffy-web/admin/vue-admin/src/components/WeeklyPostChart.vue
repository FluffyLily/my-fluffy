<template>
    <Bar :data="chartData" :options="chartOptions" />
</template>

<script setup>
import { Bar } from 'vue-chartjs'
import {
Chart as ChartJS,
Title,
Tooltip,
Legend,
BarElement,
CategoryScale,
LinearScale
} from 'chart.js'
import { ref, watch } from 'vue'

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)

const props = defineProps({
  labels: Array,
  counts: Array
})

const chartData = ref({
  labels: [],
  datasets: [
      {
        label: '게시글 수',
        data: [],
        backgroundColor: 'rgba(255, 182, 212, 0.7)',
        borderColor: '#FFB6D4',
        borderWidth: 1,
        borderRadius: 5
      }
  ]
})

watch(
  () => [props.labels, props.counts],
  ([newLabels, newCounts]) => {
    chartData.value = {
      labels: newLabels,
      datasets: [
        {
          label: '게시글 수',
          data: newCounts,
          backgroundColor: [
            '#c6b4d8', // lavender-soft
            '#cee0e6', // sky-mist
            '#f5c0bf', // peach-sherbet
            '#ead4d4', // rose-dust
            '#e36255', // coral-red
            '#a2c5c9', // mint-gray
            '#f3c262'  // golden-sand
          ],
          borderColor: '#FFB6D4',
          borderWidth: 1,
          borderRadius: 5
        }
      ]
    };
  },
  { immediate: true }
)

const chartOptions = {
  responsive: true,
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      callbacks: {
        label: ctx => ` ${ctx.raw}건`
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      suggestedMax: 5,
      ticks: {
        stepSize: 1
      }
    }
  }
}
</script>