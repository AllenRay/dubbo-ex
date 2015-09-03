/**
 * Copyright 1999-2014 dangdang.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.common.serialize.support.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoPool;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 重写 dubbox 的逻辑使用kryo 自身提供的kryo pool
 */
public class PooledKryoFactory extends KryoFactory {

    private final com.esotericsoftware.kryo.pool.KryoFactory kryoFactory = new com.esotericsoftware.kryo.pool.KryoFactory() {
        public Kryo create() {
            return createKryo();
        }
    };

    private final KryoPool pool = new KryoPool.Builder(kryoFactory).softReferences().build();



    @Override
    public void returnKryo(Kryo kryo) {
        pool.release(kryo);
    }

    @Override
    public void close() {
        //TODO why kryopool no close api.
    }

    public Kryo getKryo() {
         return pool.borrow();
    }
}
