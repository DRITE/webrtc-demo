export default function downloadFile(data: Blob, filename: string) {
    const blob = new Blob([data], {type: 'octet/stream'})
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    a.remove()
}
