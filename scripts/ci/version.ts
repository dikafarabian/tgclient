import fs from 'node:fs/promises'

const props = await fs.readFile('gradle.properties', 'utf8')
const appVerName = /^APP_VERSION_NAME=(.+)$/m.exec(props)?.[1]?.trim()
if (!appVerName) throw new Error('failed to read APP_VERSION_NAME')

const buildType = process.env.BUILD_TYPE ?? 'release'
if (buildType !== 'canary' && buildType !== 'release') throw new Error(`invalid BUILD_TYPE: ${buildType}`)

const commit = (process.env.GITHUB_SHA ?? '').slice(0, 7)
if (!/^[0-9a-f]{7}$/.test(commit)) throw new Error(`invalid GITHUB_SHA: ${process.env.GITHUB_SHA}`)

const prefix = `luminegram-${appVerName}-${commit}`
const out = {
  'apk-arm64': `${prefix}-arm64-v8a-${buildType}.apk`,
  'apk-arm7': `${prefix}-armeabi-v7a-${buildType}.apk`,
}

const githubOutput = process.env.GITHUB_OUTPUT
if (githubOutput) {
  await fs.appendFile(githubOutput, `${Object.entries(out).map(([k, v]) => `${k}=${v}`).join('\n')}\n`)
}

console.log(JSON.stringify(out, null, 2))
