import fs from 'node:fs/promises'

const props = await fs.readFile('gradle.properties', 'utf8')
const appVerName = /^APP_VERSION_NAME=(.+)$/m.exec(props)?.[1]?.trim()
if (!appVerName) throw new Error('failed to read APP_VERSION_NAME')

const out = {
  'ver-name': `${appVerName}-${(process.env.GITHUB_SHA ?? '').slice(0, 7)}`,
  'apk-arm64': `luminegram-arm64-v8a-${appVerName}-release.apk`,
  'apk-x64': `luminegram-x86_64-${appVerName}-release.apk`,
}

const githubOutput = process.env.GITHUB_OUTPUT
if (githubOutput) {
  await fs.appendFile(githubOutput, `${Object.entries(out).map(([k, v]) => `${k}=${v}`).join('\n')}\n`)
}

console.log(JSON.stringify(out, null, 2))
