class GetInfo
    prepend SimpleCommand
  
    def initialize(headers = {})
        @headers = headers
    end
  
    def call
        getuser
    end

    private

    attr_accessor :headers

    def getuser
        check = AuthorizeApiRequest.call(headers)
        check.result if check.success?
    end
end