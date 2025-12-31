package seqera.plugin

import nextflow.Session
import spock.lang.Specification

/**
 * Implements a basic factory test
 *
 */
class NfCodespacesObserverTest extends Specification {

    def 'should create the observer instance' () {
        given:
        def factory = new NfCodespacesFactory()
        when:
        def result = factory.create(Mock(Session))
        then:
        result.size() == 1
        result.first() instanceof NfCodespacesObserver
    }

}
