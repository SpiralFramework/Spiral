package org.abimon.spiral.util

operator fun SemanticVersion.compareTo(semver: SemanticVersion): Int {
    if(this.first > semver.first)
        return 1
    else if(this.first < semver.first)
        return -1

    if(this.second > semver.second)
        return 1
    else if(this.second < semver.second)
        return -1

    if(this.third > semver.third)
        return 1
    else if(this.third < semver.third)
        return -1

    return 0
}