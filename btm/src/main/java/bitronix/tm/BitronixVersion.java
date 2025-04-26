/*
 * Copyright [2023] [Bitronix Software]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bitronix.tm;

import java.util.Optional;

/**
 * Class that exposes the Bitronix version. Fetches the
 * "Implementation-Version" manifest attribute from the jar file.
 *
 * @author laingke
 */
public final class BitronixVersion {
    private BitronixVersion() {
        throw new IllegalStateException("Bitronix version utility class, unable to be instantiated");
    }

    public static String getVersion() {
        Optional<Package> pkg = Optional.ofNullable(BitronixVersion.class.getPackage());
        return pkg.map(Package::getImplementationVersion).orElse(null);
    }
}